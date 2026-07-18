import { useState, useEffect, useRef } from 'react';

const GRID_SIZE = 16; // 16x16 classic pixel art grid
const COLORS = [
    '#000000', '#FFFFFF', '#FF0000', '#00FF00', 
    '#0000FF', '#FFFF00', '#00FFFF', '#FF00FF',
    '#C0C0C0', '#808080', '#800000', '#808000',
    '#008000', '#800080', '#008080', '#000080'
];

export default function PixelGrid({ theme, timeRemaining, onSubmit }) {
    // Fill the grid with 'null' to represent pure transparency
    const [grid, setGrid] = useState(Array(GRID_SIZE * GRID_SIZE).fill(null));
    const [color, setColor] = useState('#000000'); 
    const [isDrawing, setIsDrawing] = useState(false);

    const [timeLeft, setTimeLeft] = useState(timeRemaining);
    
    const gridRef = useRef(grid);
    useEffect(() => {
        gridRef.current = grid;
    }, [grid]);

    useEffect(() => {
        if (timeLeft <= 0) {
            onSubmit(gridRef.current);
            return; 
        }
        
        const timer = setInterval(() => {
            setTimeLeft((prev) => prev - 1);
        }, 1000);
        
        return () => clearInterval(timer);
    }, [timeLeft, onSubmit]);

    const paintPixel = (index) => {
        setGrid((prevGrid) => {
            const newGrid = [...prevGrid];
            newGrid[index] = color;
            return newGrid;
        });
    };

    const handleMouseDown = (index) => {
        setIsDrawing(true);
        paintPixel(index);
    };

    const handleMouseEnter = (index) => {
        if (isDrawing) {
            paintPixel(index);
        }
    };

    const handleMouseUp = () => {
        setIsDrawing(false);
    };

    const clearGrid = () => {
        setGrid(Array(GRID_SIZE * GRID_SIZE).fill(null));
    };

    return (
        <div 
            style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
            onDragStart={(e) => e.preventDefault()} 
        >
            <div style={{ display: 'flex', justifyContent: 'space-between', width: '400px', marginBottom: '10px' }}>
                <h3 style={{ margin: 0 }}>Theme: {theme}</h3>
                <h3 style={{ margin: 0, color: '#4af626' }}>{timeLeft}s</h3>
            </div>
            
            <div 
                style={{ 
                    display: 'grid', 
                    gridTemplateColumns: `repeat(${GRID_SIZE}, 1fr)`,
                    width: '400px', 
                    height: '400px',
                    border: '2px solid #555',
                    userSelect: 'none' 
                }}
            >
                {grid.map((pixelColor, index) => {
                    // Mathematical Checkerboard Logic
                    const row = Math.floor(index / GRID_SIZE);
                    const col = index % GRID_SIZE;
                    const isEvenSquare = (row + col) % 2 === 0;
                    
                    // If pixelColor is null, show the checkerboard. Otherwise, show the paint.
                    const displayColor = pixelColor || (isEvenSquare ? '#ffffff' : '#cccccc');

                    return (
                        <div
                            key={index}
                            onMouseDown={() => handleMouseDown(index)}
                            onMouseEnter={() => handleMouseEnter(index)}
                            style={{
                                backgroundColor: displayColor,
                                cursor: 'crosshair'
                            }}
                        />
                    );
                })}
            </div>

            {/* Controls & Palette */}
            <div style={{ display: 'flex', justifyContent: 'space-between', width: '400px', marginTop: '15px' }}>
                
                {/* 16-Color Palette */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(8, 1fr)', gap: '5px', width: '310px' }}>
                    {COLORS.map((c) => (
                        <div 
                            key={c}
                            onClick={() => setColor(c)}
                            style={{
                                backgroundColor: c,
                                height: '30px',
                                border: color === c ? '3px solid #4af626' : '1px solid #555',
                                cursor: 'pointer'
                            }}
                        />
                    ))}
                </div>

                {/* Tool Buttons */}
                <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', width: '80px' }}>
                    <button 
                        onClick={() => setColor(null)} 
                        style={{
                            padding: '2px 0',
                            backgroundColor: color === null ? '#4af626' : '#333',
                            color: color === null ? '#000' : '#fff',
                            border: '1px solid #555',
                            fontSize: '12px'
                        }}
                    >
                        ERASE
                    </button>
                    <button 
                        onClick={clearGrid} 
                        style={{
                            padding: '2px 0',
                            backgroundColor: '#333',
                            color: '#ff5555',
                            border: '1px solid #555',
                            fontSize: '12px'
                        }}
                    >
                        CLEAR
                    </button>
                </div>
            </div>
        </div>
    );
}