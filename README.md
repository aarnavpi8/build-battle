# Build Battle

A real-time multiplayer drawing game inspired by Hypixel Build Battle, featuring a Java Spring Boot backend and a React web client. Players join a room, vote on a theme, draw it on a pixel grid, and rate each other's art in a series of timed rounds. The architecture is designed to be lightweight, with all game flow orchestrated server-side and pushed live to every player.

## Core Features

* **Real-Time Multiplayer:** Live bidirectional communication handled via STOMP over WebSockets, keeping every player's screen in sync as the game advances.
* **Server-Driven Game Phases:** A backend state machine sequences the lobby, theme vote, drawing, art-voting, and leaderboard rounds on precise timers, so no client can desync the game.
* **Vote-Based Themes:** Players cast a single vote each round, and the most-voted theme is selected for everyone to draw.
* **Fair Scoring:** Art ratings are applied through atomic database updates that enforce one rating per user and block voting on your own drawing.
* **Pixel Art Canvas:** A 16×16 grid with a color palette lets players create and submit drawings, which are stored and ranked on a live leaderboard by nickname.

## Prerequisites

To run this application locally, you must have the following installed and running:

* **Java:** JDK 17 or higher
* **Node.js:** v18 or higher
* **MongoDB:** Community Server running locally on default port `27017`

## Installation & Setup

### 1. Start the Database

Ensure your local MongoDB service is actively running. The backend will automatically create the required `rooms` and `drawings` collections upon first launch.

### 2. Run the Backend (Java/Spring Boot)

Navigate to the `build-battle` directory and start the server. By default, it will listen on `localhost:8080`.

```
cd build-battle
./mvnw spring-boot:run
```

### 3. Run the Client (React/Vite)

Open a new terminal window, navigate to the `build-battle-client` directory, and install the required dependencies:

```
cd build-battle-client
npm install
```

Launch the client:

```
npm run dev
```

Open the URL Vite prints (default `http://localhost:5173`).

## Usage

1. **Setup:** Open the client in two or more browser windows (use incognito/separate windows so each is a distinct player). Enter a nickname in each.
2. **Rooms:** Create a new room to get a 5-letter code, or join an existing one by entering its code. At least 2 players are needed, since self-voting is disabled.
3. **Playing:** The host starts the game. Vote on a theme, draw it on the pixel grid before the timer ends, then rate each other's drawings 1-5.
4. **Results:** After all drawings are rated, a leaderboard ranks every player by total score.

## Configuration

Phase durations are defined in `GameEngineService` (theme vote 15s, drawing 120s, 10s per drawing during art voting). Lower them while testing. If you change the data model, clear the `rooms` and `drawings` collections in MongoDB.
