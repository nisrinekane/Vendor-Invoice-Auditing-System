import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Footer from './components/FooterComponent/Footer';
import IsometricLayout from './components/IsometricLayout/IsometricLayout';
import NotFound from './components/ErrorComponent/NotFound';
import './App.css';

function App() {
    return (
        <div className="container">
            <div className="asymmetric-hero"></div> {/* Make sure this line is added */}
            <div className="main-title">Automated Invoice Audit</div>
            <div className="content">
                <Router>
                    <Routes>
                        <Route path="/" element={<IsometricLayout />} exact />
                        <Route path="*" element={<NotFound />} />
                    </Routes>
                </Router>
            </div>
            <Footer />
        </div>
    );
}


export default App;
