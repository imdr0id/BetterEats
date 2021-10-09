import React, { Component } from "react";
import PropTypes from "prop-types";
import { addEntreePost} from "../../utilities/Api";
import { message } from "antd";
export class AddEntree extends Component {
  state = {
    name: "",
    description: ""
    
  };

  onSubmit = e => {
    const restaurantId = this.props.match.params.id;
    e.preventDefault();
   // this.props.addRestaurant(this.state.name, this.state.description);
    //this.setState({ name: "", description: "", address: { addressLine: "", city: "", state: "", country: "", zipcode: "" } });
    addEntreePost(restaurantId,this.state)
    .then(res => {
     console.log(res)
     if (res.data.success === true) {
      message.success(res.data.message)
      this.props.history.push(`/restaurants/${restaurantId}`);
    } 
    })
    .catch()
  };

  onChange = e => this.setState({ [e.target.name]: e.target.value });

  render() {
    
    return (
      <div >        
        <form onSubmit={this.onSubmit} className="form">

          <input 
            type="text"
            name="name"
            className="form-item"
            placeholder="Add Entree ..."
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
            type="submit"
            value="Submit"
            className="submit"
            
          />
        </form>
      </div>
    );
  }
}

// PropTypes
AddEntree.propTypes = {
  addEntree: PropTypes.func.isRequired,
};

export default AddEntree;
