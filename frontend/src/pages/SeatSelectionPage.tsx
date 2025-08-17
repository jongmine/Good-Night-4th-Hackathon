import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

// 좌석 상태 정의
type SeatStatus = "available" | "pending" | "occupied";

// 좌석 타입 정의
interface Seat {
  id: string;
  row: number;
  col: number;
  status: SeatStatus;
  label: string; // 좌석 레이블 추가 (예: A1, B2)
}

const SeatSelectionPage: React.FC = () => {
  const navigate = useNavigate();
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([]);

  // 가상의 3x3 좌석 데이터 생성
  const initialSeats: Seat[] = Array.from({ length: 3 }, (_, rowIndex) =>
    Array.from({ length: 3 }, (_, colIndex) => {
      const rowLabel = String.fromCharCode(65 + rowIndex); // 0 -> A, 1 -> B, 2 -> C
      const colLabel = colIndex + 1;
      return {
        id: `seat-${rowLabel}${colLabel}`,
        row: rowIndex,
        col: colIndex,
        status: "available", // 초기에는 모두 예약 가능
        label: `${rowLabel}${colLabel}`, // 좌석 레이블 설정
      };
    })
  ).flat();

  const [seats, setSeats] = useState<Seat[]>(initialSeats);

  const handleSeatClick = (clickedSeat: Seat) => {
    if (clickedSeat.status === "occupied") {
      alert("이미 예약된 좌석입니다.");
      return;
    }

    setSelectedSeats((prevSelectedSeats) => {
      const isSelected = prevSelectedSeats.some(
        (seat) => seat.id === clickedSeat.id
      );
      if (isSelected) {
        // 이미 선택된 좌석이면 선택 해제
        return prevSelectedSeats.filter((seat) => seat.id !== clickedSeat.id);
      } else {
        // 선택되지 않은 좌석이면 선택
        return [...prevSelectedSeats, clickedSeat];
      }
    });
  };

  const handleReservation = () => {
    if (selectedSeats.length === 0) {
      alert("좌석을 선택해주세요.");
      return;
    }
    // 선택된 좌석 정보를 가지고 예약 폼 페이지로 이동
    navigate("/reservation-form", { state: { selectedSeats } });
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">좌석 선택</h1>
      <div className="grid grid-cols-3 gap-4 w-64 mx-auto">
        {seats.map((seat) => {
          const isSelected = selectedSeats.some((s) => s.id === seat.id);
          let seatClass = "p-4 border rounded text-center cursor-pointer";
          if (seat.status === "available") {
            seatClass += " bg-green-200 hover:bg-green-300";
          } else if (seat.status === "pending") {
            seatClass += " bg-yellow-200";
          } else if (seat.status === "occupied") {
            seatClass += " bg-red-400 cursor-not-allowed";
          }

          if (isSelected) {
            seatClass += " ring-2 ring-blue-500"; // 선택된 좌석에 파란색 테두리 추가
          }

          return (
            <div
              key={seat.id}
              className={seatClass}
              onClick={() => handleSeatClick(seat)}
            >
              {seat.label} {/* 좌석 레이블 표시 */}
            </div>
          );
        })}
      </div>
      <div className="mt-8 text-center">
        <h2 className="text-xl font-semibold mb-2">선택된 좌석:</h2>
        {selectedSeats.length > 0 ? (
          <p>{selectedSeats.map((s) => s.label).join(", ")}</p>
        ) : (
          <p>선택된 좌석이 없습니다.</p>
        )}
        <button
          onClick={handleReservation}
          className="mt-4 px-6 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          예약하기
        </button>
      </div>
    </div>
  );
};

export default SeatSelectionPage;
