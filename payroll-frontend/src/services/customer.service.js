import httpClient from '../http-common.js';


const completeProfile = () => {
  return httpClient.post('/customer/complete-profile');
};

const getAllCustomers = () => {
  return httpClient.get('/customer/all');
};

const findCustomerByRut = (rut) => {

  return httpClient.get(`/customer/findCustomer?rut=${encodeURIComponent(rut)}`);
};

const getCustomersWithLoansGreaterThan = (quantity) => {

  return httpClient.get(`/customer/allGreatherThan?quantity=${quantity}`);
};

const updateRestriction = (rut) => {
  return httpClient.put(`/customer/update-restriction?rut=${encodeURIComponent(rut)}`);
};

const getOverdueCustomers = () => {

  return httpClient.get('/customer/overdue');
};

export default {
  completeProfile,
  getAllCustomers,
  findCustomerByRut,
  getCustomersWithLoansGreaterThan,
  getOverdueCustomers,
  updateRestriction,
};
