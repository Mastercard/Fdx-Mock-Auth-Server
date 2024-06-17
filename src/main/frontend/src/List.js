import React from 'react';

const List = (listprops) => {
  const { accounts } = listprops;

  let preSelectedAccountList = [];
  if (window.prevAccountIds && (window.prevAccountIds.length > 0)) {
      preSelectedAccountList = window.prevAccountIds.split(",");
  }

  let [selected, setSelected] = React.useState(preSelectedAccountList);
  const requestedScopes = window.scopes.replace(/[ ,]+/g, ", ");
  const currentDate = new Date();
  currentDate.setSeconds(window.sharingDuration);
  const formattedDate = currentDate.toLocaleString("en-US",{weekday :'short', year:'numeric', month:'short', day:'numeric'});

  const handleSubmit = (event) => {
    event.preventDefault();
    if(selected.length < 1){
      document.getElementById('error-message').style.display = 'block';
    } else {
      document.getElementById("clientId").value = window.clientId;
      document.getElementById("state").value = window.state;
      document.getElementById("scopes").value = window.scopes;
      document.getElementById("accountIds").value = selected;
      document.getElementById("consentShareDurationSeconds").value = window.sharingDuration;
      document.getElementById("prevConsentId").value = window.prevConsentId;
      document.getElementById("consent_form").submit();
    }
  };

  const handleCancel = (event) => {
    event.preventDefault();
    document.getElementById("cancelConsent").value = "user_cancelled_consent";
    document.getElementById("clientId").value = window.clientId;
    document.getElementById("state").value = window.state;
    document.getElementById("scopes").value = window.scopes;
    document.getElementById("consentShareDurationSeconds").value = window.sharingDuration;
    document.getElementById("prevConsentId").value = window.prevConsentId;
    document.getElementById("consent_form").submit();
  };

  const handleOnChange = (e) => {
    // Destructuring
    const { value, checked } = e.target;

    // Case 1 : The user checks the box
    if (checked) {
      setSelected([...selected, value]);
      document.getElementById('error-message').style.display = 'none';
    }
    // Case 2  : The user unchecks the box
    else {
      setSelected(selected.filter((e) => e !== value));
    }
  };

  if (!accounts || accounts.length === 0) return <p>No accounts, sorry</p>;

  let amendConsentLabel;
  if (window.amendConsent) {
    amendConsentLabel = (<p className='text-align-left'>Amending consent for Consent ID: <b>{window.prevConsentId}</b></p>);
  } else
  {
    amendConsentLabel = (<p></p>);
  }

  return (
    <ul>
      {amendConsentLabel}
      <p className='text-align-left'>Your consent will expire on <b>{formattedDate}</b></p>
      <p className='text-align-left'>Requested Scopes - <b>{requestedScopes}</b></p>
      <div className="error-message" id="error-message">
        Please select atleast one account to provide consent.
      </div>
      <h2 className='list-head'>Available Accounts</h2>
      <form id="consent_form" method="post" action="/consent">
        <input type="hidden" id="clientId" name="clientId" value="" />
        <input type="hidden" id="state" name="state" value="" />
        <input type="hidden" id="scopes" name="scopes" value="" />
        <input type="hidden" id="accountIds" name="accountIds" value="" />
        <input type="hidden" id="consentShareDurationSeconds" name="consentShareDurationSeconds" value="" />
        <input type="hidden" id="prevConsentId" name="prevConsentId" value="" />
        <input type="hidden" id="cancelConsent" name="cancelConsent" value="" />

      </form>
      <form onSubmit={handleSubmit} className="form-box">
        <div className="text-align-left">
          Please select one or more accounts to give consent to: <br/><br/>
          <div className="accounts">
          {accounts.map((account, i) => {
            return (
              <React.Fragment>
                <input
                  type="checkbox"
                  id={account.accountId}
                  value={account.accountId}
                  onChange={handleOnChange}
                  defaultChecked={selected.includes(account.accountId)}
                />&nbsp;&nbsp;
                <label for="{account.accountId}">
                  <span className='account-id'>{account.nickname} ({account.accountId}) </span>
                  <span className='account-name'>{account.accountNumberDisplay + " : " + account.productName}</span>
                </label>
                <br/>
              </React.Fragment>
            );
          })}
          </div>
          <br/>
        </div>
        <input type="submit" value="Submit" />
        &nbsp;
        <input type="button" onClick={handleCancel} value="Cancel" />
      </form>
    </ul>
  );
};
export default List;