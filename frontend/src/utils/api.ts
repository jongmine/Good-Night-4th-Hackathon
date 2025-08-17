import { useClientToken } from '../context/ClientTokenContext';

const API_BASE_URL = '/api'; // Assuming API is served from the same origin

interface RequestOptions extends RequestInit {
  includeClientToken?: boolean;
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

  return { callApi };
};
