# IntelliCal

IntelliCal is an Android calorie-tracking app built with Kotlin and Jetpack Compose. Users can photograph a meal, receive an AI-generated nutrition estimate, track nutrition goals, and grow a virtual forest as they build healthy habits.

Demo: https://youtube.com/shorts/UCFAg8bHJJc

## Features

- AI-assisted meal analysis using Google Gemini
- Email authentication and PostgreSQL persistence through Supabase
- Daily calorie and macro tracking
- Goal and progress management
- Virtual forest and achievement system
- Local mock database for development and testing

## Tech Stack

- Kotlin, Jetpack Compose, Material 3, and Compose Navigation
- Supabase Auth and PostgREST
- Google Gemini API
- Ktor, Kotlinx Serialization, Gson, and Coil
- JUnit and MockK

## Project Attribution

IntelliCal was developed as a four-person University of Waterloo course project by Pranav Vajpeyi, Jerry Lin, Saad Zaki, and Sujal Chouhan.

See [`AUTHORS.md`](AUTHORS.md) for project credits. This portfolio copy excludes course contracts, grading instructions, private wiki content, generated build files, and credentials.

## Academic Integrity

This repository is shared for portfolio and educational reference purposes. Do not submit this project, its code, or closely derived work as your own coursework. Anyone using this repository is responsible for following their institution's academic-integrity policies.

Public distribution should only occur with the approval of every project team member.

## Prerequisites

- Android Studio with the Android SDK
- JDK 11 or newer
- An Android emulator or device running API 26+
- A personal Supabase project
- A personal Google Gemini API key

## Local Setup

1. Clone this repository and open it in Android Studio.
2. Create your local configuration:

   ```bash
   ./setup.sh
   ```

3. Open `local.properties` and replace the placeholders:

   ```properties
   sdk.dir=/absolute/path/to/your/Android/sdk
   supabase.url=https://YOUR_PROJECT_REF.supabase.co
   supabase.key=YOUR_SUPABASE_ANON_KEY
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY
   ```

4. Create a new Supabase project.
5. In the Supabase SQL editor, run [`db/schema.sql`](db/schema.sql).
6. Optionally run [`db/seed.sql`](db/seed.sql) for generic sample data.
7. In Supabase Authentication, enable email authentication.
8. Create a Gemini API key in Google AI Studio and restrict it to the APIs and applications you intend to use.
9. Build and test:

   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```

10. Run the app from Android Studio or install it on a connected device:

    ```bash
    ./gradlew installDebug
    ```

## Supabase Configuration

Use the public Supabase **anon key**, never a service-role key. This application is an educational prototype and its database schema does not include production-ready authorization policies. Before exposing a Supabase project publicly:

- Enable Row Level Security for every table.
- Add policies that restrict users to their own records.
- Do not store real health or sensitive personal information.
- Use a separate disposable development project.

## Gemini Security

Android applications cannot securely hide API keys because packaged application values can be extracted. For local development, use a restricted, low-quota Gemini key. A production deployment should call Gemini through a secured backend rather than directly from the Android client.

## Development Without External Services

The project contains `MockDatabase` and `MockNutritionEstimationService` implementations for development and tests. To run the full app without Supabase, switch the `USE_MOCK_DATA` constant in `MainActivity.kt` to `true`.

## Repository Safety

- `local.properties` is ignored and must never be committed.
- Never commit API keys, passwords, service-role keys, or real user data.
- Run a secret scanner before publishing changes.
- This repository intentionally contains no license. Add one only after all team members agree on distribution terms.
