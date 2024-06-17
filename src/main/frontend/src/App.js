import React, { useEffect, useState } from 'react';
import "./App.css";
import List from './List.js';
import ButtonAppBar from './AppBar.js';
import withLoading from './WithLoading.js';


function App() {
  const ListLoading = withLoading(List);
  const [appState, setAppState] = useState({
    loading: false,
    accounts: null,
    checkedState:null,
  });

  useEffect(() => {
    setAppState({ loading: true });

    var myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");

    var requestOptions = {
        method: 'GET',
        headers: myHeaders, 
        redirect: 'follow'
    };

    const apiUrl = `/consent/accounts`;

    fetch(apiUrl, requestOptions)
      .then((res) => res.json())
      .then((result) => {
      	console.log(result);
        setAppState({ loading: false, accounts: result[0].accounts, checkedState:new Array(result[0].accounts.length).fill(false) });
      });
  }, [setAppState]);


  return (
    <div className='App'>
      <ButtonAppBar />
      <div className='accounts-container'>
        <h1>My Accounts</h1>
      </div>
      <ListLoading isLoading={appState.loading} accounts={appState.accounts} />
    </div>
  );
}



/*
function App() {
  return (
    <div className="App">
      <ButtonAppBar />
      <Adr />
      <Stepper />
    </div>
  );
}
*/

export default App; 