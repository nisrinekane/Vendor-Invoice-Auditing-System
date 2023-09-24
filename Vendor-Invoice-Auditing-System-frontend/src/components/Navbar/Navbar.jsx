import React from 'react'
import './Navbar.css'

const Navbar = () => {
    return (
        <nav className="navbar">
            <a href="/" className="navbar-logo">AutoAudit</a>
            <div className="navbar-links">
                <a href="#features">Features</a>
                <a href="#pricing">Pricing</a>
                <a href="#contact">Contact</a>
            </div>
        </nav>
    );
};

export default Navbar;