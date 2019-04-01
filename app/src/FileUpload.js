import React from "react";
import { Component } from "react";
import { Progress } from "reactstrap";
import axios from "axios";
import "./FileUpload.css";
import SelectBox from "./features/select-box";

class FileUpload extends Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedFile: null,
            configurations: null,
            isConfig: this.props.type === "csv" ? false : true,
            csvConfigurationName: "cvs",
            listCSVConfigurationName: []
        };
        getCSVConfigurationName();
        /*
        axios.get("http://localhost:8088/api/listConfigurationName").then(res => {
            for (var i = 0; i < res.data.length; i++)
                this.state.listCSVConfigurationName[i] = { value: res.data[i], id: i + 1 };
        });
        */
    }

    function getCSVConfigurationName() {

        axios.get("http://localhost:8088/api/listConfigurationName").then(res => {
            var list = [];
            for (var i = 0; i < res.data.length; i++)
                list[i] = { value: res.data[i], id: i + 1 };
            setState({ listCSVConfigurationName: list });
        });
    }
    
    componentWillReceiveProps() {
        this.setState.isConfig = this.props.type === "csv" ? false : true;
        if (!this.state.isConfig) {
            getCSVConfigurationName();
        } else {
            this.setState({ listCSVConfigurationName: [] });
        }
    }

    onChangeHandler = event => {
        this.setState({
            selectedFile: event.target.files,
            loaded: 0
        });
    };

    onClickHandler = () => {
        const data = new FormData();
        for (var i = 0; i < this.state.selectedFile.length; i++) {
            data.append("files", this.state.selectedFile[i]);
        }

        if (this.state.isConfig) {
            axios
                .post("http://localhost:8088/api/uploadMultiConfig", data, {
                    onUploadProgress: ProgressEvent => {
                        this.setState({
                            loaded: (ProgressEvent.loaded / ProgressEvent.total) * 100
                        });
                    }
                })
                .then(res => {
                    // then print response status
                    console.log(res);
                });
        } else {
            var csvData = {
                configuration_name: "cvs",
                files: data
            };
            axios
                .post("http://localhost:8088/api/uplodMultiCSVFile", csvData, {
                    onUploadProgress: ProgressEvent => {
                        this.setState({
                            loaded: (ProgressEvent.loaded / ProgressEvent.total) * 100
                        });
                    }
                })
                .then(res => {
                    // then print response status
                    console.log(res);
                });
        }
    };

    render() {
        return (
            <div class="container">
                <div class="row">
                    <div class="col-md-6">
                        <form method="post" action="#" id="#">
                            <div class="form-group files">
                                <label>Upload Configuration File(s)</label>
                                <input type="file" class="form-control" multiple onChange={this.onChangeHandler} />
                            </div>
                        </form>
                        <div class="form-group">
                            <Progress max="100" color="success" value={this.state.loaded}>
                                {Math.round(this.state.loaded, 2)}%
                            </Progress>
                        </div>
                        <button type="button" class="btn btn-success btn-block" onClick={this.onClickHandler}>
                            Upload
                        </button>
                    </div>
                </div>
                <div>
                    {this.state.listCSVConfigurationName.length === 0 ? null : (
                        <div class="row">
                            <div class="col-md-6">
                                <div>
                                    <h3>CSV Configuration</h3>
                                    <div style={{ margin: "16px", position: "relative" }} />
                                    <SelectBox items={this.state.listCSVConfigurationName} />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        );
    }
}

export default FileUpload;
