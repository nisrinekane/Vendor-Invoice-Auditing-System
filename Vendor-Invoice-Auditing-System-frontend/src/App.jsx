import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Footer from './components/FooterComponent/Footer';
// import IsometricLayout from './components/IsometricLayout/IsometricLayout';
import FileUploadForm from './components/FileUploadForm/FileUploadForm';
import NotFound from './components/Error/NotFound';
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
                        <Route path="/" element={<FileUploadForm />} />
                        <Route path="*" element={<NotFound />} />
                    </Routes>
                </div>
                <Footer />
            </Router>
        </div>
    );
}

export default App;
