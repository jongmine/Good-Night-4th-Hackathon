import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { v4 as uuidv4 } from 'uuid';

interface ClientTokenContextType {
  clientToken: string | null;
}

const ClientTokenContext = createContext<ClientTokenContextType | undefined>(undefined);

export const ClientTokenProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [clientToken, setClientToken] = useState<string | null>(null);

  useEffect(() => {
    let token = localStorage.getItem('clientToken');
    if (!token) {
      token = uuidv4();
      localStorage.setItem('clientToken', token);
    }
    setClientToken(token);
  }, []);

  return (
    <ClientTokenContext.Provider value={{ clientToken }}>
      {children}
    </ClientTokenContext.Provider>
  );
};

export const useClientToken = () => {
  const context = useContext(ClientTokenContext);
  if (context === undefined) {
    throw new Error('useClientToken must be used within a ClientTokenProvider');
  }
  return context;
};
