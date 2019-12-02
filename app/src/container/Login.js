import React, { useState } from "react";
import { Button, FormGroup, FormControl, FormLabel } from "react-bootstrap";
import axios from "axios";
import "./Login.css";

export default function Login(props) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [conf, setConf] = useState("");

    function validateForm() {
        return username.length > 0 && password.length > 0;
    }

    async function handleSubmit(event) {
        event.preventDefault();

        try {
            // await Auth.signIn(username, password);
            await authenticate(username, password);
            alert("Logged in");
        } catch (e) {
            alert(e.message);
        }
    }

    function authenticate(username, password) {
        var url = "/api/login?username=" + username + "&password=" + password;
        axios.get(url).then(res => {
            var config = res.data;
            console.log("Config: " + config);
            setConf(config);
            // props.userHasAuthenticated(res.data);
        });
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
