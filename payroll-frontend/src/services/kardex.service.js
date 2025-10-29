import httpClient from '../http-common.js';


const getToolHistory = (toolName) => {
  return httpClient.get(`/kardex/tool-history?toolName=${toolName}`);
};


const getMovementsInRange = (from, to, movementType) => {
  return httpClient.get(`/kardex/range?from=${from}&to=${to}&movementType=${movementType}`);
};


const getActiveLoansGrouped = () => {
  return httpClient.get('/kardex/loans/active/grouped');
};


const getTopTools = (from, to, limit) => {
  const params = new URLSearchParams();
  if (from) params.append('from', from);
  if (to) params.append('to', to);
  if (limit) params.append('limit', limit);
  return httpClient.get(`/kardex/tools/top?${params.toString()}`);
};

const getAllKardex = () => {
  return httpClient.get('/kardex/all');
};

export {
  getToolHistory,
  getMovementsInRange,
  getActiveLoansGrouped,
  getTopTools,
  getAllKardex
};