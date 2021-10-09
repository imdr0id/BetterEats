import React, { Component } from "react";
import PropTypes from "prop-types";
import "./AddRestaurant.css";
import { addRestaurantPost} from "./../../utilities/Api";
import { message } from "antd";
export class AddRestaurant extends Component {
  state = {
    name: "",
    description: "",
    address: {}
  };

  onSubmit = e => {
    e.preventDefault();
   // this.props.addRestaurant(this.state.name, this.state.description);
    //this.setState({ name: "", description: "", address: { addressLine: "", city: "", state: "", country: "", zipcode: "" } });
    addRestaurantPost(this.state)
    .then(res => {
     console.log(res)
     message.success("Restaurant Added");
     this.props.history.push(`/`);
    })
    .catch()
  };

  onChange = e => this.setState({ [e.target.name]: e.target.value });

  render() {
    
    return (
      <div >        
        <form onSubmit={this.onSubmit} class="form">

          <input 
            type="text"
            name="name"
            style={{ flex: "7", padding: "5px" }}
            placeholder="Add Restaurant ..."
            value={this.state.name}
            onChange={this.onChange}
          />
          <input
            type="text"
            name="description"
            style={{ flex: "7", padding: "5px" }}
            placeholder="Add Description ..."
            value={this.state.description}
            onChange={this.onChange}
          />
          <input
            type="text"
            name="addressLine"
            style={{ flex: "7", padding: "5px" }}
            placeholder="Add Address line ..."
            value={this.state.address.addressLine}
            onChange={this.onChange}
          />
          <input
            type="text"
            name="city"
            style={{ flex: "7", padding: "5px" }}
            placeholder="Add City ..."
            value={this.state.address.city}
            onChange={this.onChange}
          />
          <input
            type="text"
            name="state"
            style={{ flex: "7", padding: "5px" }}
            placeholder="Add State ..."
            value={this.state.address.state}
            onChange={this.onChange}
          />
          <input
            type="text"
            name="country"
            style={{ flex: "7", padding: "5px" }}
            placeholder="Add Country ..."
            value={this.state.address.country}
            onChange={this.onChange}
          />
          <input
            type="text" pattern="[0-9]{6}"
            name="zipcode"
            style={{ flex: "7", padding: "5px" }}
            placeholder="Add zipcode ..."
            value={this.state.address.zipcode}
            onChange={this.onChange}
          />
          <input
            type="submit"
            value="Submit"
            class="submit"
            
          />
        </form>
      </div>
    );
  }
}

// PropTypes
AddRestaurant.propTypes = {
  addRestaurant: PropTypes.func.isRequired,
};

export default AddRestaurant;
