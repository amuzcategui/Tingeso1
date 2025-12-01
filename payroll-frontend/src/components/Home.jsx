import React, { useState, useEffect, useRef } from 'react';
import * as toolService from '../services/tool.service.js';
import customerService from '../services/customer.service.js';
import { Link } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import '../index.css';
import { useCart } from './CartContext.jsx'; 
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import CheckIcon from '@mui/icons-material/Check';
import "./Menu.jsx"

const Home = () => {
  const [allTools, setAllTools] = useState([]);
  const [filteredTools, setFilteredTools] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchPerformed, setSearchPerformed] = useState(false);

  const { keycloak, initialized } = useKeycloak();
  const hasSyncedRef = useRef(false);
  const { addToCart, cartItems } = useCart();

  useEffect(() => {

    const run = async () => {
      if (!initialized) return;
      if (!keycloak.authenticated) { setIsLoading(false); return; }

      try {
        if (!hasSyncedRef.current) {
          await customerService.completeProfile(); 
          hasSyncedRef.current = true;
        }
        const response = await toolService.getAllTools();
        setAllTools(response.data);
      } catch (err) {
        const backendMsg = err?.response?.data;
        console.error('Fallo al sincronizar o cargar datos:', backendMsg || err.message, err);
        setError(backendMsg || 'No se pudieron cargar los datos.');
      } finally {
        setIsLoading(false);
      }
    };
    
    run();
  }, [initialized, keycloak.authenticated]);

  const handleSearch = (event) => {
    const searchTerm = event.target.value;
    setSearchPerformed(true);
    if (searchTerm === '') {
      setFilteredTools([]);
    } else {
      const filtered = allTools.filter(tool =>
        tool.name.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredTools(filtered);
    }
  };

  if (!initialized || isLoading) return <div>Cargando...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="home">
      <h1>ToolRent</h1>
      <p>Sistema de arrendamiento de herramientas. </p>
      <p>¡Encuentra la herramienta que necesitas para tu proyecto! </p>
      <div className="search-wrap">
      <input
        type="text"
        placeholder="Buscar herramienta por nombre..."
        onChange={handleSearch}
        className="search-input"
      />

      {searchPerformed && (
        <>
          {filteredTools.length > 0 ? (
            <table className="tools-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Categoría</th>
                  <th>Valor de Reposición</th>
                  <th>Valor de Arriendo</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {filteredTools.map((tool) => {
              
                  const isInCart = cartItems.some(item => item.id === tool.id);
                  return (
                    <tr key={tool.id}>
                      <td>{tool.id}</td>
                      <td>{tool.name}</td>
                      <td>{tool.category}</td>
                      <td>${tool.rentalFee}</td>
                      <td>${tool.toolValue}</td>
                      <td>

                        <button 
                          
                          className="action-button"
                          onClick={() => addToCart(tool)}
                          disabled={isInCart} 
                          
                        >
                          {isInCart ? <CheckIcon /> : <AddShoppingCartIcon />}
                        </button>
                    </td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          ) : (
            <p>No se encontraron herramientas con ese nombre.</p>
          )}
        </>
      )}
      </div>
    </div>
  );
};

export default Home;
