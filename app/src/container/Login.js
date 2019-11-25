import React, { useState } from "react";
import { Button, FormGroup, FormControl, FormLabel } from "react-bootstrap";
import "./Login.css";

export default function Login(props) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    function validateForm() {
        return username.length > 0 && password.length > 0;
    }

    function signIn(username, password) {
        return true;
    }

    async function handleSubmit(event) {
        event.preventDefault();

        try {
            // await Auth.signIn(username, password);
            await signIn(username, password);
            alert("Logged in");
        } catch (e) {
            alert(e.message);
        }
    }

    /*
    async function handleSubmit(event) {
        event.preventDefault();

        try {
            await AuthenticatorAssertionResponse.signIn(username, password);
            props.userHasAuthenticated(true);
            alert("Logged in ...");
        } catch (e) {
            alert(e.message);
        }
    }
    */

    return (
        <div className="Login">
            <form onSubmit={handleSubmit}>
                <FormGroup controlId="UsernameLabel">
                    <FormLabel>Username</FormLabel>
                </FormGroup>
                <FormGroup controlId="username">
                    <FormControl
                        autoFocus
                        placeholder="Username"
                        type="text"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                    />
                </FormGroup>
                <FormGroup controlId="PasswordLabel">
                    <FormLabel>Password</FormLabel>
                </FormGroup>
                <FormGroup controlId="password">
                    <FormControl
                        placeholder="Password"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                    />
                </FormGroup>
                <Button block disabled={!validateForm()} type="submit">
                    Login
                </Button>
            </form>
        </div>
    );
}
