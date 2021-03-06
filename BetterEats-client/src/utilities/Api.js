import axios from "axios";

// set up jwt calls each time
axios.interceptors.request.use(
  config => {
    // console.log(config);
    // baseURL is supposed to be for sending only to internal links
    //   if (config.baseURL === baseApiAddress && !config.headers.Authorization) {
    if (!config.headers.Authorization) {
      const token = localStorage.accessToken;

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  error => Promise.reject(error)
);

export function signUpPost(values) {
  return axios.post("/api/auth/signup", values);
}

export function signUpOwner(values) {
  return axios.post("/api/auth/owner/signup", values);
}

export function signInPost(values) {
  return axios.post("/api/auth/signin", values);
}

export function validateUniqueUsernameGet(value) {
  return axios.get(`/api/user/checkUsernameAvailability?username=${value}`);
}

export function validateUniqueEmailGet(value) {
  return axios.get(`/api/user/checkEmailAvailability?email=${value}`);
}

export function currentUserGet() {
  if (!localStorage.accessToken) {
    return Promise.reject("No access token.");
  }

  return axios.get("/api/user/me");
}

export function singleRestaurantGet(id) {
  return axios.get(`/api/restaurants/${id}`);
}

export function addReviewPost(entreeId, values) {
  return axios.post(`/api/entrees/${entreeId}/review`, values);
}

export function addRestaurantPost(values) {
  return axios.post(`/api/restaurants`, values);
}

export function addEntreePost(restaurantId,values) {  
  return axios.post(`/api/restaurants/${restaurantId}/entree`, values);
  
}

export function searchAndGet(values) {
  return axios.get(`/api/search`,{
    params: {
      search: values
    }
  });
}