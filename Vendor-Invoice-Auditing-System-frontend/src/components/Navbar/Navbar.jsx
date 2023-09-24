import React from 'react'
import './Navbar.css'
import logo from './logo.png'

const Navbar = () => {
    return (
        <nav className="navbar">
            <div className="navbar-logo-title">
                <img className="navbar-logo" src={logo} alt="logo" />
                <a href="/" className="navbar-title">AutoAudit</a>
            </div>
            <div className="navbar-links">
                <a href="#features">Features</a>
                <a href="#pricing">Pricing</a>
                <a href="#contact">Contact</a>
            </div>
        </nav>
    );
};

export default Navbar;
