import React, { useState } from "react";
import { Button, FormGroup, FormControl, FormLabel } from "react-bootstrap";
import "./Login.css";

export default function Login(props) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    function validateForm() {
        console.error("Username: " + username + "\nPassword: " + password);
        return username.length > 0 && password.length > 0;
    }

    async function handleSubmit(event) {
        event.preventDefault();
        try {
            // await Auth.signIn(username, password);
            alert("Logged in");
        } catch (e) {
            alert(e.message);
        }
    }
    
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
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        type="password"
                    />
                </FormGroup>
                <Button block disabled={!validateForm()} type="submit">
                    Login
                </Button>
            </form>
        </div>
    );
}
