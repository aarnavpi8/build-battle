// import { useState, useEffect } from 'react';
// import { Client } from '@stomp/stompjs';
// import SockJS from 'sockjs-client';

// export function useGameEngine(roomId, playerId) {
//     const [stompClient, setStompClient] = useState(null);
//     const [connected, setConnected] = useState(false);
//     const [gameState, setGameState] = useState({
//         phase: 'LOBBY',
//         durationSeconds: 0,
//         data: null
//     });

//     useEffect(() => {
//         if (!roomId) return;

//         const client = new Client({
//             // Ensure this points to your Spring Boot server port
//             webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
//             debug: (str) => console.log(str), // Turn off in production
//             onConnect: () => {
//                 setConnected(true);
//                 // Listen to the backend state machine
//                 client.subscribe(`/topic/room/${roomId}/phase`, (message) => {
//                     const payload = JSON.parse(message.body);
//                     setGameState({
//                         phase: payload.phase,
//                         durationSeconds: payload.durationSeconds,
//                         data: payload.data
//                     });
//                 });
//             },
//             onStompError: (frame) => {
//                 console.error('Broker error: ' + frame.headers['message']);
//             }
//         });

//         client.activate();
//         setStompClient(client);

//         // Cleanup on unmount
//         return () => client.deactivate();
//     }, [roomId]);

//     const startGame = () => {
//         if (stompClient && connected) {
//             stompClient.publish({ destination: `/app/room/${roomId}/start`, body: JSON.stringify({}) });
//         }
//     };

//     const submitDrawing = (gridData) => {
//         if (stompClient && connected) {
//             stompClient.publish({ 
//                 destination: `/app/room/${roomId}/submit`, 
//                 body: JSON.stringify({ grid: gridData }) 
//             });
//         }
//     };

//     const submitArtVote = (drawingId, score) => {
//         if (stompClient && connected) {
//             stompClient.publish({ 
//                 destination: `/app/room/${roomId}/art-vote`, 
//                 body: JSON.stringify({ 
//                     userId: "player_1", // Make this dynamic later
//                     drawingId: drawingId,
//                     score: score
//                 }) 
//             });
//         }
//     };

//     // Update your return statement to include it:
//     return { connected, gameState, startGame, submitDrawing, submitArtVote };
// }

import { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// playerId is the identity used for every action the user takes in the room.
export function useGameEngine(roomId, playerId) {
    const [stompClient, setStompClient] = useState(null);
    const [connected, setConnected] = useState(false);
    const [gameState, setGameState] = useState({
        phase: 'LOBBY',
        durationSeconds: 0,
        data: null
    });

    useEffect(() => {
        if (!roomId) return;

        const client = new Client({
            // Ensure this points to your Spring Boot server port
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            debug: (str) => console.log(str), // Turn off in production
            onConnect: () => {
                setConnected(true);
                // Listen to the backend state machine
                client.subscribe(`/topic/room/${roomId}/phase`, (message) => {
                    const payload = JSON.parse(message.body);
                    setGameState({
                        phase: payload.phase,
                        durationSeconds: payload.durationSeconds,
                        data: payload.data
                    });
                });
            },
            onStompError: (frame) => {
                console.error('Broker error: ' + frame.headers['message']);
            }
        });

        client.activate();
        setStompClient(client);

        // Cleanup on unmount
        return () => client.deactivate();
    }, [roomId]);

    const publish = (suffix, body) => {
        if (stompClient && connected) {
            stompClient.publish({
                destination: `/app/room/${roomId}/${suffix}`,
                body: JSON.stringify(body)
            });
        }
    };

    const startGame = () => publish('start', {});

    // Backend ThemeVotePayload = { userId, theme }
    const submitThemeVote = (theme) => publish('theme-vote', { userId: playerId, theme });

    // Backend DrawingPayload record = { userId, pixels }
    const submitDrawing = (pixels) => publish('submit-drawing', { userId: playerId, pixels });

    // Backend ArtVotePayload = { userId, drawingId, score }
    const submitArtVote = (drawingId, score) =>
        publish('art-vote', { userId: playerId, drawingId, score });

    return { connected, gameState, startGame, submitThemeVote, submitDrawing, submitArtVote };
}
