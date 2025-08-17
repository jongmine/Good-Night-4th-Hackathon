import { useClientToken } from '../context/ClientTokenContext';

const API_BASE_URL = '/api'; // Assuming API is served from the same origin

interface RequestOptions extends RequestInit {
  includeClientToken?: boolean;
}

// 백엔드에서 받아올 좌석 타입 정의
export interface Seat {
  id: string;
  label: string; // 예: A1, B2
  status: 'AVAILABLE' | 'PENDING' | 'OCCUPIED'; // 백엔드 상태값에 맞춰 대문자로 변경
}

// 백엔드에서 받아올 예약 타입 정의
export interface Reservation {
  id: string;
  seatIds: string[];
  name: string;
  contact: string;
  status: 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
}

export const useApi = () => {
  const { clientToken } = useClientToken();

  const callApi = async <T>(
    endpoint: string,
    options: RequestOptions = {}
  ): Promise<T> => {
    const { includeClientToken = true, headers, ...rest } = options;

    const config: RequestInit = {
      ...rest,
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
    };

    if (includeClientToken && clientToken) {
      config.headers = {
        ...config.headers,
        'X-Client-Token': clientToken,
      };
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || 'Something went wrong');
    }

    return response.json() as Promise<T>;
  };

  // 모든 좌석 정보를 가져오는 API
  const getSeats = (): Promise<Seat[]> => {
    return callApi<Seat[]>('/seats');
  };

  // 좌석을 예약하는 API
  const createReservation = (seatIds: string[], name: string, contact: string): Promise<Reservation> => {
    return callApi<Reservation>('/reservations', {
      method: 'POST',
      body: JSON.stringify({ seatIds, name, contact }),
    });
  };

  // 예약을 취소하는 API
  const cancelReservation = (reservationId: string): Promise<void> => {
    return callApi<void>(`/reservations/${reservationId}`, {
      method: 'DELETE',
    });
  };

  // SSE를 통해 실시간 좌석 업데이트를 받는 함수
  const connectToSeatUpdates = (callback: (seat: Seat) => void): EventSource => {
    const eventSource = new EventSource(`${API_BASE_URL}/sse/seat-updates`);

    eventSource.onmessage = (event) => {
      const updatedSeat: Seat = JSON.parse(event.data);
      callback(updatedSeat);
    };

    eventSource.onerror = (error) => {
      console.error('EventSource error:', error);
      eventSource.close();
    };

    return eventSource;
  };

  return { callApi, getSeats, createReservation, cancelReservation, connectToSeatUpdates };
};