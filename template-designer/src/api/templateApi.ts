import httpClient from './httpClient';
import type { Template } from '../types/template';

export const templateApi = {
  list: () => httpClient.get('/templates'),
  get: (id: string) => httpClient.get(`/templates/${id}`),
  create: (template: Template) => httpClient.post('/templates', { template }),
  update: (id: string, template: Template) => httpClient.put(`/templates/${id}`, { template }),
  delete: (id: string) => httpClient.delete(`/templates/${id}`),
  clone: (id: string) => httpClient.post(`/templates/${id}/clone`),
};

export const generateApi = {
  inline: (template: Template, data: object) =>
    httpClient.post('/generate/inline', { template, data }, { responseType: 'blob' }),
};
