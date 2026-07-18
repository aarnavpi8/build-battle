import { useState } from 'react';
import { useGameEngine } from './useGameEngine';
import PixelGrid from './PixelGrid';
import './App.css'; // We will clean this up next

function App() {
    const [roomInput, setRoomInput] = useState('');
    const [activeRoomId, setActiveRoomId] = useState(null);

    // This hook manages the entire connection lifecycle autonomously 
    const { connected, gameState, startGame, submitDrawing, submitArtVote } = useGameEngine(activeRoomId);

    const handleCreateRoom = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/rooms', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ hostId: "reactUser123" })
            });
            const data = await response.json();
            setActiveRoomId(data.id); // Instantly connect to the new room
        } catch (error) {
            console.error("Failed to create room. Is Spring Boot running?", error);
        }
    };

    // 1. LOBBY SCREEN (Before joining a WebSocket room)
    if (!activeRoomId) {
        return (
            <div className="container">
                <h1>Build Battle</h1>
                <div>
                    <input 
                        type="text" 
                        placeholder="Enter 5-Letter Code" 
                        value={roomInput}
                        onChange={(e) => setRoomInput(e.target.value.toUpperCase())}
                        maxLength={4}
                    />
                    <button onClick={() => setActiveRoomId(roomInput)}>Join Room</button>
                </div>
                
                {/* The new Create button */}
                <div style={{ marginTop: '20px' }}>
                    <button onClick={handleCreateRoom}>Create New Room</button>
                </div>
            </div>
        );
    }

    // 2. WAITING ROOM SCREEN (Connected, waiting to start)
    if (gameState.phase === 'LOBBY') {
        return (
            <div className="container">
                <h2>Room: {activeRoomId}</h2>
                <p className="status">Status: {connected ? 'Connected (Waiting for host)' : 'Connecting...'}</p>
                <button onClick={startGame} disabled={!connected}>
                    Start Game
                </button>
            </div>
        );
    }

    // 3. THEME VOTE SCREEN
    if (gameState.phase === 'THEME_VOTE') {
        return (
            <div className="container">
                <h2>Vote for a Theme!</h2>
                <p>Time remaining: {gameState.durationSeconds}s</p>
                <div className="theme-grid">
                    {gameState.data.map((theme, idx) => (
                        <button key={idx}>{theme}</button>
                    ))}
                </div>
            </div>
        );
    }

    // 4. DRAWING SCREEN
    if (gameState.phase === 'DRAWING') {
        return (
            <div className="container">
                <PixelGrid 
                    theme={gameState.data} 
                    timeRemaining={gameState.durationSeconds} 
                    onSubmit={submitDrawing} 
                />
            </div>
        );
    }

    // 5. ART VOTE SCREEN
    if (gameState.phase === 'ART_VOTE') {
        const drawing = gameState.data; // The backend sends a single Drawing object here
        
        return (
            <div className="container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <h2>Rate this art!</h2>
                <h3 style={{ color: '#4af626', margin: '0 0 20px 0' }}>{gameState.durationSeconds}s</h3>
                
                {/* Notice we pass drawing.pixels, not the whole object */}
                <PixelViewer gridData={drawing.pixels} size={400} />
                
                {/* Minimalist 1-5 Scoring Buttons */}
                <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                    {[1, 2, 3, 4, 5].map((score) => (
                        <button 
                            key={score}
                            onClick={() => submitArtVote(drawing.id, score)}
                            style={{ 
                                padding: '10px 20px', 
                                backgroundColor: '#333', 
                                color: '#fff', 
                                border: '1px solid #555',
                                cursor: 'pointer'
                            }}
                        >
                            {score}
                        </button>
                    ))}
                </div>
            </div>
        );
    }

    // 6. LEADERBOARD SCREEN
    if (gameState.phase === 'LEADERBOARD') {
        const rankedDrawings = gameState.data; // The backend sends a List<Drawing> sorted by score

        return (
            <div className="container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <h2>Final Results</h2>
                
                <div style={{ display: 'flex', gap: '30px', marginTop: '20px', flexWrap: 'wrap', justifyContent: 'center' }}>
                    {rankedDrawings.map((drawing, index) => (
                        <div key={drawing.id} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                            <h3 style={{ color: index === 0 ? '#FFD700' : '#FFF' }}>
                                #{index + 1} ({drawing.totalScore} pts)
                            </h3>
                            <p style={{ margin: '0 0 10px 0', fontSize: '14px', color: '#aaa' }}>{drawing.userId}</p>
                            
                            <PixelViewer gridData={drawing.pixels} size={150} />
                        </div>
                    ))}
                </div>
                
                <button onClick={() => window.location.reload()} style={{ marginTop: '40px', padding: '10px 20px' }}>
                    Play Again
                </button>
            </div>
        );
    }

    // Fallback Catch-all
    return (
        <div className="container">
            <h2>{gameState.phase}</h2>
            <pre>{JSON.stringify(gameState.data, null, 2)}</pre>
        </div>
    );
}

export default App;