GymLog - Minimalist Workout Tracking App
GymLog is a clean, minimalist workout tracking application designed to simplify your fitness journey. With its intuitive interface and essential features, GymLog helps you focus on what matters most - your workout performance.
Features

Clean Design: Distraction-free black and white interface
Workout Tracking: Log sets, reps, and weights for each exercise
Custom Routines: Create and save your favorite workout routines
Exercise Library: Comprehensive database of exercises categorized by muscle groups
Quick Start: Jump straight into a workout with minimal setup
Google Authentication: Securely sync your data across devices

Tech Stack
GymLog is built with modern Android development practices:

UI: Jetpack Compose for a fully declarative UI
Architecture: MVVM with Clean Architecture principles
Dependency Injection: Hilt
Database: Room for local persistence
Remote Storage: Firebase Firestore
Authentication: Firebase Authentication

Screenshots

Getting Started

Clone this repository
Open the project in Android Studio Arctic Fox or newer
Connect your Firebase project (add google-services.json)
Build and run the application

Project Structure
The application follows Clean Architecture principles with the following layers:

Data: Repositories, data sources, and models
Domain: Use cases, domain models, and repository interfaces
Presentation: ViewModels, UI components, and screens

Planned Features

Workout history and statistics
Progress charts and performance analytics
Rest timer functionality
Export/import workout data
Body measurements tracking
Dark/light theme toggle
