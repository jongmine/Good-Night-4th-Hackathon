import React from 'react';
import { Routes, Route } from 'react-router-dom';
import SeatSelectionPage from './pages/SeatSelectionPage';
import ReservationFormPage from './pages/ReservationFormPage';
import './App.css';

function App() {
  return (
    <div className="App">
      <Routes>
        <Route path="/" element={<SeatSelectionPage />} />
        <Route path="/reserve/:seatId" element={<ReservationFormPage />} />
      </Routes>
    </div>
  );
}

export default App;