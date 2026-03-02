import axios from 'axios';

const httpClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' }
});

export default httpClient;
