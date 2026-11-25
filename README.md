# MyWallet – Control de gastos con Firebase

Aplicación Android desarrollada en **Java** con arquitectura **MVVM**, basada en el proyecto de ejemplo *AppFirebase*.
Permite registrar, consultar, modificar y eliminar los gastos personales en **Cloud Firestore**. Cada usuario inicia sesión con **Firebase Authentication** y solo ve sus propios gastos.

## 1. Idea de la aplicación

| | |
|---|---|
| **Nombre** | MyWallet |
| **Propósito** | Llevar el control de los gastos diarios y conocer el total gastado. |
| **Colección** | `usuarios/{uid}/gastos/{idGasto}` |
| **Campos** | `id`, `descripcion`, `monto`, `categoria`, `fecha`, `metodoPago` |
| **Ingreso de datos** | Formulario con campos de texto, calendario para la fecha y listas desplegables (Spinner) para la categoría y el método de pago. |
| **Visualización** | Lista con todos los gastos del usuario (se actualiza en tiempo real) y el total gastado. |

## 2. Estructura del proyecto (igual que AppFirebase)

```
app/src/main/java/com/devst/misgastos/
├── model/
│   └── Gasto.java              → Datos de un gasto (constructor vacío + getters/setters)
├── repositorio/
│   ├── GastoRepositorio.java   → CRUD en Cloud Firestore
│   └── AuthRepositorio.java    → Registro, login y logout con Firebase Authentication
├── viewmodel/
│   ├── MainViewModel.java      → Validaciones y LiveData de los gastos
│   └── AuthViewModel.java      → Validaciones y LiveData de la sesión
└── ui/
    ├── LoginActivity.java      → Inicio de sesión (pantalla inicial)
    ├── RegistroActivity.java   → Crear cuenta
    └── MainActivity.java       → Formulario CRUD, lista y cerrar sesión
```

**¿En qué parte del código se crea la información?** En `GastoRepositorio.java`:

```java
private static final String COLLECTION_NAME = "gastos";
private FirebaseFirestore db = FirebaseFirestore.getInstance();

db.collection("usuarios").document(uid).collection(COLLECTION_NAME)
  .document(id)
  .set(gasto);
```

- `COLLECTION_NAME`: nombre de la colección.
- `getInstance()`: obtiene la conexión configurada con Firestore.
- `document(uid)`: separa los datos por usuario usando su UID.
- `.document(id).set(gasto)`: crea el documento, o lo sobrescribe si ya existe. La colección se crea con la primera escritura.

## 3. Operaciones CRUD

| Operación | Botón | Método del repositorio | Firestore |
|---|---|---|---|
| Crear | Guardar | `createOrUpdateGasto()` | `.set()` |
| Leer | Buscar / lista | `getGasto()` / `listenGastos()` | `.get()` / `.addSnapshotListener()` |
| Actualizar | Actualizar | `createOrUpdateGasto()` | `.set()` |
| Eliminar | Eliminar | `deleteGasto()` | `.delete()` |

**Validaciones** (en el ViewModel): campos obligatorios, monto numérico y mayor a 0, fecha con formato dd/mm/aaaa, correo válido, contraseña de al menos 6 caracteres y confirmación de contraseña. Cada resultado se informa con un Toast.

## 4. Configuración de Firebase

1. Crear un proyecto en [Firebase Console](https://console.firebase.google.com).
2. Agregar una app Android con el paquete **`com.devst.misgastos`**.
3. Descargar `google-services.json` y copiarlo en la carpeta `app/`.
4. Habilitar **Firestore Database** y pegar las reglas del archivo [`firestore.rules`](firestore.rules).
5. Habilitar **Authentication → Correo electrónico/contraseña**.

---

# Informe de investigación: Firebase Authentication

### 1. ¿Qué es Firebase Authentication y para qué sirve?
Es un servicio de Firebase que gestiona la identidad de los usuarios de una aplicación. Ofrece servicios en la nube y SDK listos para registrar usuarios, iniciar sesión y cerrar sesión con distintos métodos: correo y contraseña, teléfono, Google, Facebook, etc. Sirve para saber **quién** usa la aplicación sin tener que programar ni mantener un servidor propio de usuarios. Firebase se encarga de guardar las credenciales de forma segura, validar las contraseñas y mantener la sesión abierta aunque se cierre la app.

### 2. ¿Cuál es la diferencia entre Authentication y Firestore?
- **Authentication** responde *quién es el usuario*: administra cuentas, contraseñas y sesiones, y entrega a cada usuario un identificador único (UID).
- **Cloud Firestore** responde *qué datos guarda la aplicación*: es una base de datos NoSQL en la nube organizada en colecciones y documentos (en esta app, los gastos).

Ambos servicios trabajan juntos. Authentication identifica al usuario y Firestore usa ese UID, mediante sus reglas de seguridad, para decidir qué datos puede leer o escribir.

### 3. ¿Cómo se registra e inicia sesión con correo y contraseña?
1. Habilitar el proveedor *Correo electrónico/contraseña* en Firebase Console.
2. Agregar la dependencia `firebase-auth` y obtener la instancia con `FirebaseAuth.getInstance()`.
3. **Registro:** `auth.createUserWithEmailAndPassword(email, password)`. Si tiene éxito, el usuario queda creado y con la sesión iniciada.
4. **Inicio de sesión:** `auth.signInWithEmailAndPassword(email, password)`.
5. **Usuario conectado:** `auth.getCurrentUser()` devuelve el usuario actual, o `null` si no hay sesión.
6. **Cerrar sesión:** `auth.signOut()`.

Todas estas operaciones son asíncronas, así que el resultado se recibe con `addOnSuccessListener` y `addOnFailureListener` (ver `AuthRepositorio.java`).

### 4. ¿Qué es el UID de un usuario y cómo puede relacionarse con sus datos?
El **UID** (*User ID*) es un identificador único que Firebase asigna a cada cuenta, y nunca cambia. Se obtiene con `auth.getCurrentUser().getUid()`. Sirve para relacionar al usuario con su información. En MyWallet, los gastos se guardan en la ruta `usuarios/{uid}/gastos/{idGasto}`, de modo que cada usuario tiene su propia sub-colección. Las reglas de seguridad comparan ese UID con el del usuario que hace la petición (`request.auth.uid`).

### 5. ¿Por qué no se deben guardar contraseñas en documentos de Firestore?
- Firestore guarda los datos tal como se envían. Una contraseña quedaría en texto plano, visible en la consola y para cualquiera con acceso a la base de datos o a un respaldo.
- Si las reglas fallan o hay una filtración, las contraseñas quedarían expuestas, y muchas personas reutilizan la misma contraseña en otros servicios.
- Firebase Authentication ya guarda las contraseñas de forma segura: las almacena con *hash* y *salt* (algoritmo scrypt modificado), nunca las devuelve y se encarga de validarlas.
- Por eso esta app solo envía la contraseña a Firebase Authentication y nunca la escribe en Firestore.

### 6. ¿Qué reglas de seguridad ayudan a proteger los registros?
Las **Firebase Security Rules** se ejecutan en el servidor y deciden si cada lectura o escritura se permite. En MyWallet (archivo `firestore.rules`):
- **Exigir autenticación:** `request.auth != null`, para que nadie sin sesión pueda acceder.
- **Restringir por dueño:** `request.auth.uid == uid`, para que cada usuario solo acceda a sus propios gastos.
- **Validar los datos:** que el monto sea un entero mayor a 0, que la descripción no esté vacía y que solo existan los campos permitidos (`keys().hasOnly(...)`).
- **Denegar todo lo demás:** `match /{document=**} { allow read, write: if false; }`.
- **No usar el "modo de prueba"** (`allow read, write: if true;` o con fecha de expiración), porque deja la base de datos abierta a cualquier persona.

### Fuentes consultadas
- Firebase. *Firebase Authentication*. https://firebase.google.com/docs/auth
- Firebase. *Authenticate with Firebase using Password-Based Accounts on Android*. https://firebase.google.com/docs/auth/android/password-auth
- Firebase. *Manage Users in Firebase (Android)*. https://firebase.google.com/docs/auth/android/manage-users
- Firebase. *Cloud Firestore*. https://firebase.google.com/docs/firestore
- Firebase. *Get started with Cloud Firestore Security Rules*. https://firebase.google.com/docs/firestore/security/get-started
- Firebase. *Writing conditions for Cloud Firestore Security Rules*. https://firebase.google.com/docs/firestore/security/rules-conditions
- Firebase. *Password hashing (scrypt)*. https://firebaseopensource.com/projects/firebase/scrypt/
- Material de apoyo del docente: `FIEREBASE.pdf` y proyecto de ejemplo `AppFirebase.zip`.
