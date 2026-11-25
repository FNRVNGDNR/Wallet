# 💰 MyWallet — Control de gastos personales

Aplicación Android desarrollada en **Java** que permite a cada usuario registrar, consultar, modificar y eliminar sus gastos personales. Los datos se guardan en **Cloud Firestore** y el acceso se protege con **Firebase Authentication** (correo y contraseña).

> Actividad práctica · Desarrollo de aplicaciones móviles

---

## 📱 Funcionalidades

- **Registro e inicio de sesión** con correo y contraseña (Firebase Authentication).
- **Sesión persistente**: si el usuario ya inició sesión, la app entra directo a sus gastos.
- **CRUD completo de gastos**:
  - **Crear**: formulario con ID, descripción, monto, fecha, categoría y método de pago.
  - **Leer**: lista en tiempo real de los gastos del usuario y búsqueda por ID.
  - **Actualizar**: al tocar un gasto de la lista se carga en el formulario para editarlo.
  - **Eliminar**: con el botón Eliminar o manteniendo presionado un gasto de la lista.
- **Total gastado** y cantidad de registros, calculados automáticamente.
- **Calendario** (DatePicker) para elegir la fecha.
- **Validaciones** de campos obligatorios, formato de correo, contraseña mínima, monto numérico mayor a 0 y fecha válida.
- **Mensajes** (Toast) que informan el resultado de cada operación.
- **Cierre de sesión**.

## 🗂️ Modelo de datos

Cada usuario tiene sus propios gastos, relacionados mediante su **UID** de Firebase Authentication:

```
usuarios/{uid}/gastos/{idGasto}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | String | Identificador del gasto (ej: G001) |
| `descripcion` | String | Detalle del gasto (ej: Almuerzo) |
| `monto` | Number (entero) | Monto en pesos chilenos, mayor a 0 |
| `categoria` | String | Comida, Transporte, Hogar, Salud, Entretención, Educación u Otros |
| `fecha` | String | Formato `dd/MM/aaaa` |
| `metodoPago` | String | Efectivo, Débito, Crédito o Transferencia |

Las contraseñas **no se guardan en Firestore**: las administra Firebase Authentication.

## 🏗️ Arquitectura (MVVM)

```
com.devst.misgastos
├── model/
│   └── Gasto.java                 → Modelo del documento de Firestore
├── repositorio/
│   ├── AuthRepositorio.java       → Registro, login y logout (Firebase Authentication)
│   └── GastoRepositorio.java      → Operaciones CRUD (Cloud Firestore)
├── viewmodel/
│   ├── AuthViewModel.java         → Validaciones de login y registro
│   └── MainViewModel.java         → Validaciones y lógica de gastos
└── ui/
    ├── LoginActivity.java         → Pantalla de inicio de sesión
    ├── RegistroActivity.java      → Pantalla de registro
    └── MainActivity.java          → Formulario, lista y total de gastos
```

- **Model**: representa los datos.
- **Repositorio**: único lugar que se comunica con Firebase.
- **ViewModel**: valida y expone el estado a la vista con `LiveData`.
- **UI (Activities)**: muestra la información y observa los cambios.

## 🔒 Reglas de seguridad de Firestore

El archivo [`firestore.rules`](firestore.rules) garantiza que:

- Solo un usuario autenticado puede acceder a datos.
- Cada usuario solo puede leer, crear, modificar y eliminar **sus propios** gastos (`request.auth.uid == uid`).
- Los datos guardados deben ser válidos: campos permitidos, descripción no vacía, monto entero mayor a 0.
- Cualquier otra ruta de la base de datos queda bloqueada.

## 🛠️ Tecnologías

- Java 11 · Android SDK (minSdk 31, targetSdk 36)
- Android Studio · Gradle (Kotlin DSL)
- Firebase BoM · Cloud Firestore · Firebase Authentication
- AndroidX: AppCompat, Material Components, ConstraintLayout, Lifecycle (ViewModel + LiveData)

## 🚀 Cómo ejecutar el proyecto

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/FNRVNGDNR/Wallet.git
   ```
2. Crear un proyecto en [Firebase Console](https://console.firebase.google.com) y registrar una app Android con el paquete `com.devst.misgastos`.
3. Descargar `google-services.json` y copiarlo en la carpeta `app/` (no se incluye en el repositorio).
4. En Firebase Console:
   - **Authentication** → habilitar el proveedor *Correo electrónico/contraseña*.
   - **Firestore Database** → crear la base de datos y publicar el contenido de `firestore.rules` en la pestaña *Reglas*.
5. Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutar en un emulador o dispositivo con Android 12 o superior.

## 📸 Capturas

| Inicio de sesión | Registro | Gastos |
|---|---|---|
| ![Login](docs/capturas/login.png) | ![Registro](docs/capturas/registro.png) | ![Gastos](docs/capturas/gastos.png) |

| Cloud Firestore | Firebase Authentication |
|---|---|
| ![Firestore](docs/capturas/firestore.png) | ![Authentication](docs/capturas/authentication.png) |

## 👤 Autor

**Luis** — Estudiante de Informática, Santo Tomás.
