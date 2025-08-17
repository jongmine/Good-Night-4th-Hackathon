import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import './index.css';
import { BrowserRouter } from 'react-router-dom';
import { ClientTokenProvider } from './context/ClientTokenContext';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <ClientTokenProvider>
        <App />
      </ClientTokenProvider>
    </BrowserRouter>
  </React.StrictMode>,
);