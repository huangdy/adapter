import React from "react";
import { Switch, Route } from "react-router-dom";
import Home from "./container/Home";
import Login from "./container/Login";
import NotFound from "./container/NotFound";
import AppliedRoute from "./components/AppliedRoute";

export default function Routes({ appProps }) {
    return (
        <Switch>
            <AppliedRoute path="/" exact component={Home} appProps={appProps} />
            <AppliedRoute path="/login" exact component={Login} appProps={appProps} />
            {/* Finally, catch all unmatched routes */}
            <Route component={NotFound} />
        </Switch>
    );
}
