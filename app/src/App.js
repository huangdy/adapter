import React from "react";
// import { render } from "react-dom";

import Tabs from "./Tabs";
import ConfigFileUpload from "./ConfigFileUpload";
import CSVFileUpload from "./CSVFileUpload";
require("./Tab.css");

function App() {
  return (
    <div>
      <h1>Saber Adapter</h1>
      <Tabs>
        <div label="Configuration File Upload">
          <ConfigFileUpload />
        </div>
        <div label="CSV File Upload">
          <CSVFileUpload />
        </div>
        <div label="Query">
          Nothing to see here, this tab is <em>extinct</em>!
        </div>
      </Tabs>
    </div>
  );
}

export default App;
