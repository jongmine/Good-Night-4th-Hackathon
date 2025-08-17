import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

// SeatSelectionPage에서 정의된 Seat 타입과 동일하게 정의
interface Seat {
  id: string;
  row: number;
  col: number;
  status: 'available' | 'pending' | 'occupied';
  label: string; // 좌석 레이블 추가 (SeatSelectionPage와 동일하게)
}

const ReservationFormPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const selectedSeats: Seat[] = location.state?.selectedSeats || [];

  const [name, setName] = useState('');
  const [contact, setContact] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (selectedSeats.length === 0) {
      alert('선택된 좌석이 없습니다. 좌석 선택 페이지로 돌아갑니다.');
      navigate('/');
      return;
    }

    if (!name.trim() || !contact.trim()) {
      alert('이름과 연락처를 모두 입력해주세요.');
      return;
    }

    // TODO: 실제 백엔드 API 호출 로직 구현
    console.log('예약 정보 제출:', {
      selectedSeats,
      name,
      contact,
    });

    alert('예약이 성공적으로 접수되었습니다! (실제 예약은 아님)');
    navigate('/'); // 예약 완료 후 메인 페이지로 이동
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">예약 정보 입력</h1>

      <div className="mb-4">
        <h2 className="text-xl font-semibold mb-2">선택된 좌석:</h2>
        {selectedSeats.length > 0 ? (
          <p className="text-lg">
            {selectedSeats.map((s) => s.label).join(', ')}
          </p>
        ) : (
          <p className="text-red-500">선택된 좌석이 없습니다.</p>
        )}
      </div>

      <form onSubmit={handleSubmit} className="bg-white p-6 rounded shadow-md">
        <div className="mb-4">
          <label htmlFor="name" className="block text-gray-700 text-sm font-bold mb-2">
            이름:
          </label>
          <input
            type="text"
            id="name"
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            value={name}
            onChange={(e) => setName(e.target.value)}n            required
          />
        </div>
        <div className="mb-6">
          <label htmlFor="contact" className="block text-gray-700 text-sm font-bold mb-2">
            연락처:
          </label>
          <input
            type="text"
            id="contact"
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            value={contact}
            onChange={(e) => setContact(e.target.value)}n            required
          />
        </div>
        <div className="flex items-center justify-between">
          <button
            type="submit"
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
          >
            예약 제출
          </button>
          <button
            type="button"
            onClick={() => navigate('/')}
            className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
          >
            뒤로 가기
          </button>
        </div>
      </form>
    </div>
  );
};

export default ReservationFormPage;