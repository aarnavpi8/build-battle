const GRID_SIZE = 16;

export default function PixelViewer({ gridData, size = 300 }) {
    // Failsafe in case the backend sends empty data
    if (!gridData || gridData.length !== GRID_SIZE * GRID_SIZE) {
        return <div style={{ color: '#ff5555' }}>No artwork data available.</div>;
    }

    return (
        <div 
            style={{ 
                display: 'grid', 
                gridTemplateColumns: `repeat(${GRID_SIZE}, 1fr)`,
                width: `${size}px`, 
                height: `${size}px`,
                border: '2px solid #555',
                userSelect: 'none' 
            }}
        >
            {gridData.map((pixelColor, index) => {
                const row = Math.floor(index / GRID_SIZE);
                const col = index % GRID_SIZE;
                const isEvenSquare = (row + col) % 2 === 0;
                
                const displayColor = pixelColor || (isEvenSquare ? '#ffffff' : '#cccccc');

                return (
                    <div
                        key={index}
                        style={{ backgroundColor: displayColor }}
                    />
                );
            })}
        </div>
    );
}