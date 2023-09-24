import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Footer from './components/FooterComponent/Footer';
import IsometricLayout from './components/IsometricLayout/IsometricLayout';
import NotFound from './components/ErrorComponent/NotFound';
import Navbar from './components/Navbar/Navbar';
import Header from './components/Header/Header';
import './App.css';

function App() {
    return (
        <div className="container">
            <Router>
                <Navbar />
                <Header />
                <div className="content">
                    <Routes>
                        <Route path="/" element={<IsometricLayout />} exact />
                        <Route path="*" element={<NotFound />} />
                    </Routes>
                </div>
                <Footer />
            </Router>
        </div>
    );
}

export default App;
