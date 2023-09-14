import React from 'react';
import GitHubIcon from '@mui/icons-material/GitHub';
import LinkedInIcon from '@mui/icons-material/LinkedIn';
import EmailIcon from '@mui/icons-material/Email';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-container">
                <div className="footer-link">
                    <EmailIcon className="icon" />
                    <a href="mailto:nisrine@nisrinekane.com">Email</a>
                </div>
                <div className="footer-link">
                    <GitHubIcon className="icon" />
                    <a href="https://github.com/nisrinekane">GitHub</a>
                </div>
                <div className="footer-link">
                    <LinkedInIcon className="icon" />
                    <a href="https://www.linkedin.com/in/nisrinekane/">LinkedIn</a>
                </div>
            </div>
            <p className="footer-copyright">&copy; {new Date().getFullYear()}</p>
        </footer>
    );
};

export default Footer;
