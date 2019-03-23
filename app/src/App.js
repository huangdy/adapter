import React from "react";
import Tabs from "./Tabs";
import FileUpload from "./FileUpload";
require("./Tab.css");

function App() {
  return (
    <div>
      <h1>Saber Adapter</h1>
      <Tabs>
        <div label="Configuration File Upload">
          <FileUpload />
        </div>
        <div label="CSV File Upload">
          <FileUpload type="csv" />
        </div>
        <div label="Query">
          Nothing to see here, this tab is <em>extinct</em>!
        </div>
      </Tabs>
    </div>
  );
}

export default App;
