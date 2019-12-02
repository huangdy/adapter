import React, { useState } from "react";
import { Navbar, NavItem, Nav } from "react-bootstrap";
import { LinkContainer } from "react-router-bootstrap";
import Routes from "./Routes";
import logo from "./logo.svg";
import "./App.css";

export default function App(props) {
    const [userRole, setUserRole] = useState("");

    function handleLogout() {
        setUserRole("");
    }

    return (
        <div className="App container">
            <Navbar collapseOnSelect expand="lg" variant="dark" bg="primary">
                <Navbar.Brand href="/">
                    <img src={logo} alt="" height="75px" width="125px" />
                </Navbar.Brand>
                {userRole != "" ? (
                    <NavItem onClick={handleLogout}>Logout</NavItem>
                ) : (
                    <>
                        <LinkContainer to="/login">
                            <NavItem>Login</NavItem>
                        </LinkContainer>
                    </>
                )}
            </Navbar>
            <Routes appProps={{ userRole, setUserRole }} />
        </div>
    );
}
