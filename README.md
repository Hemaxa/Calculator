# Calculator

A simple calculator for Android built using Kotlin and Jetpack Compose.

## Light Theme

<img width="2560" height="1440" alt="1" src="https://github.com/user-attachments/assets/bb48221f-9514-4192-962b-6d2725382aaf" />

## Dark Theme

<img width="2560" height="1440" alt="2" src="https://github.com/user-attachments/assets/6b5392ee-571e-40ff-a76a-c7b0098455eb" />

## Features

- **Basic operations:** addition, subtraction, multiplication, and division.
- **Extra functions:** square root (√), power (xʸ), and sign change (±).
- **Adaptive interface:** automatically adjusts the button layout for portrait and landscape modes.
- **Themes:** supports three color schemes (Blue, Orange, Pink) that can be switched from the menu.
- **Light/Dark mode:** themes automatically adapt to system settings.
- **State saving:** the app remembers the current input, active operation, and selected theme when closed.
- **Scientific notation:** displays very large or very small numbers in exponential format (e.g., 1.2345E7).
- **Repeat operation:** pressing "=" again repeats the last performed operation.


## Technologies Used

- **Kotlin:** main programming language.
- **Jetpack Compose:** modern UI toolkit for building native Android interfaces.
- **ViewModel:** manages calculator logic and state independently from the UI lifecycle.
- **LiveData:** passes state (current input) from the ViewModel to Composable functions.
- **SharedPreferences:** stores calculator state and selected theme between sessions.
