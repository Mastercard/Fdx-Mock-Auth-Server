import React from "react";
import "./App.css";
import { adrData } from "./data";

export const Adr = () => {
    return (
        <>
            <div className="adr-container">
                
                {adrData.map((data, key) => {
                    return (
                        <div key={key}>
                            {data.name} ADR is requesting data from your accounts.<br/>
                            <br/>.<br/>
                        </div>
                    );
                })}

            </div>
        </>
    );
};
