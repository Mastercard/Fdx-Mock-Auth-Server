import React from "react";
import "./App.css";
import { accountData } from "./data";


const date1 = new Date()

const date2 =  new Date(date1.setMonth(date1.getMonth()+12));


export const Accounts = (accountprops) => {




    const accounts  = accountprops.accounts || {};
    const checkedState = accountprops.checkedState || {};

    

    const handleOnChange = (position) => {
//        const updatedCheckedState = checkedState.map((item, index) =>
//            index === position ? !item : item
//        );
//
//        setCheckedState(updatedCheckedState);
    };

    return (
        <>
            <div className="account-container">

                {accounts.data.accounts.map((data, key) => {
                    return (
                        <div key={key}>
                            {data.name + " {" + data.number + "} "}

                            <input
                                type="checkbox"
                                id={`custom-checkbox-${key}`}
                                checked={checkedState[key]}
                                onChange={() => handleOnChange(key)}
                            />
                        </div>
                    );
                })}
                <br/>
            </div>
        </>
    );
};

export const AccountsConfirm = () => {
    return (
        <>
            <div className="accounts-confirm-container">

                {accountData.map((data, key) => {
                    return (
                        <div key={key}>
                            {data.name}
                        </div>
                    );
                })}

                <br />
                <div>Data will be shared between: {new Date().toString()} and {date2.toString()} </div>


            </div>
        </>
    );
};



