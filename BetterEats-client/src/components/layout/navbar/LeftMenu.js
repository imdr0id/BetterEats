import React, { Component } from 'react';
import { Menu } from 'antd';
import { Link } from "react-router-dom";


class LeftMenu extends Component {



  addMenu = () => {  
    console.log(this.props.currentUser)  
    if (this.props.currentUser && this.props.currentUser.role === "ROLE_ADMIN") {     
      return( 
      <Menu.Item key="createRestaurant">
        <Link to="/createRestaurant">Add Restaurant</Link>
      </Menu.Item>
      )
    }else return null;
  }


  render() {
    return (
      <Menu mode={this.props.mode}>
        <Menu.Item key="home">
          <Link to="/">Home</Link>
        </Menu.Item>
        {this.addMenu()}       
        <Menu.Item key="about">
          <Link to="/about">About</Link>
        </Menu.Item>
      </Menu>
    );
  }
}

export default LeftMenu;