import React, { Component } from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import NavBar from "./components/layout/navbar/NavBar";
import NotFound from "./components/layout/NotFound";
import Restaurants from "./components/restaurants/RestaurantsNew";
import AddRestaurant from "./components/restaurants/AddRestaurant";
import EntreesTable from "./components/entrees/EntreesTable";
import AddEntree from "./components/entrees/AddEntree";
import ReviewsTable from "./components/reviews/ReviewsTable";
import ReviewsAddForm from "./components/reviews/ReviewsAddForm";
import About from "./components/pages/About";
import Login from "./components/pages/Login";
import RegisterUser from "./components/pages/RegisterUser";
import RegistrationSuccess from "./components/pages/RegistrationSuccess";
import axios from "axios";
import { currentUserGet, searchAndGet } from "./utilities/Api";
import { message } from "antd";

import { Input } from "antd";


import "./App.css";



const { Search } = Input;

class App extends Component {
  state = {
    restaurants: [],
    currentUser: null,
    isAuthenticated: false,
    searchKey:""
  };

  componentDidMount() {
    if(this.state.isAuthenticated)
    this.getUser();
    axios
      .get("/api/restaurants")
      .then(res => this.setState({ restaurants: res.data.content }));
  }

  setLogout = () => {
    localStorage.removeItem("accessToken");
    this.setState({
      currentUser: null,
      isAuthenticated: false
    });
    message.success("Logout successful!");
  };

  setLogin = () => {
    this.getUser();
    message.success("Login successful!");
  };

  getUser = () => {
    currentUserGet().then(user =>
      this.setState({
        currentUser: user.data,
        isAuthenticated: true
      })
    ).catch((error) => message.error(error));
  };

  // Toggles To Complete
  markComplete = id => {
    this.setState({
      restaurants: this.state.restaurants.map(restaurant => {
        if (restaurant.id === id) {
          restaurant.add_entree = !restaurant.add_entree;
        }
        return restaurant;
      })
    });
  };

  delRestaurant = id => {
    axios.delete(`/api/restaurants/${id}`).then(res =>
      this.setState({
        restaurants: [
          ...this.state.restaurants.filter(restaurant => restaurant.id !== id)
        ]
      })
    );
  };

  addRestaurant = (name, description) => {
    axios
      .post("/api/restaurants", {
        name,
        description
        // }).then(res => console.log(res.data));
      })
      .then(res =>
        this.setState({ restaurants: [...this.state.restaurants, res.data] })
      );
  };

  onSearch = (e) => {

    searchAndGet(e).then(res => {
      console.log(res)
      console.log(res.data.length)
      if (res.data.length > 0) {
        this.setState({ restaurants: res.data })
      }else{
        this.setState({ restaurants: [] })
        console.log(this.state)
        message.error('No results')
      }
    }

    ).catch((error) => message.error(error));

  }


  render() {
    return (
      <Router>
        <div className="App">
          <div className="container">
            <NavBar
              currentUser={this.state.currentUser}
              setLogout={this.setLogout}
            />
            <Switch>
              <Route
                exact
                path="/"
                render={props => (
                  <React.Fragment>

                    <Search
                      placeholder="Search here"
                      allowClear
                      style={{ padding: "30px" }}
                      enterButton="Search"
                      size="large"
                      onSearch={this.onSearch}
                    />
                    {/* {<Restaurants
                      restaurants={this.state.restaurants}
                      markComplete={this.markComplete}
                      delRestaurant={this.delRestaurant}
                    />} */}
                    <Restaurants user={this.state.currentUser}  />
                  </React.Fragment>
                )}
              />
              <Route path="/about" component={About} />
              {/* <Route exact path="/user/login" component={Login} /> */}
              <Route
                exact
                path="/user/login"
                render={props => <Login setLogin={this.setLogin} />}
              />
              <Route exact path="/user/create" component={RegisterUser} />
              <Route
                path="/user/create/success"
                component={RegistrationSuccess}
              />
              <Route
                path="/restaurants/:id/entree/create"
                component={AddEntree}
              />
              <Route
                path="/restaurants/:id"
                component={EntreesTable}
              />
              <Route
                path="/createRestaurant"
                component={AddRestaurant}
              />
              <Route exact
                path="/entrees/:id"
                component={ReviewsTable}
              />
              <Route
                path="/entrees/:id/reviews/create"
                component={ReviewsAddForm}
              />
              <Route component={NotFound} />
            </Switch>
          </div>
        </div>
      </Router>
    );
  }
}

export default App;
