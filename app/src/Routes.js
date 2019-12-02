import React from "react";
import { Switch, Route } from "react-router-dom";
import Home from "./container/Home";
import Login from "./container/Login";
import NotFound from "./container/NotFound";
import Upload from "./components/FileUpload";
import AppliedRoute from "./components/AppliedRoute";

export default function Routes({ appProps }) {
    return (
        <Switch>
            <AppliedRoute path="/" exact component={Home} appProps={appProps} />
            <AppliedRoute path="/login" exact component={Login} appProps={appProps} />
            <AppliedRoute path="/upload" exact component={Upload} appProps = {appProps} />
            {/* Finally, catch all unmatched routes */}
            <Route component={NotFound} />
        </Switch>
    );
}
