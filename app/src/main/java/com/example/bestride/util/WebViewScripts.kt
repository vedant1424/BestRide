// WebViewScripts.kt
package com.example.bestride.util

object WebViewScripts {
    fun buildPhoneNumberScript(phoneNumber: String): String = """
        (function() {
            function injectPhoneAndClick() {
                const phoneInput = document.querySelector('input[aria-label="Enter phone number or email"]') || 
                                 document.querySelector('div[class*="css"] input') ||
                                 document.querySelector('input[placeholder*="phone"]');
                
                if (phoneInput) {
                    console.log('Found phone input, injecting number...');
                    phoneInput.value = '+91$phoneNumber';
                    phoneInput.dispatchEvent(new Event('input', { bubbles: true }));
                    phoneInput.dispatchEvent(new Event('change', { bubbles: true }));
                    
                    setTimeout(() => {
                        const continueButton = Array.from(document.getElementsByTagName('button'))
                            .find(button => 
                                button.textContent.toLowerCase().includes('continue') &&
                                !button.textContent.toLowerCase().includes('google') &&
                                !button.textContent.toLowerCase().includes('apple')
                            );
                        
                        if (continueButton) {
                            console.log('Found continue button, clicking...');
                            continueButton.click();
                        } else {
                            console.log('Continue button not found, retrying...');
                            setTimeout(injectPhoneAndClick, 500);
                        }
                    }, 500);
                } else {
                    console.log('Phone input not found, retrying...');
                    setTimeout(injectPhoneAndClick, 500);
                }
            }
            injectPhoneAndClick();
        })();
    """.trimIndent()

    fun buildEmailScript(email: String): String = """
        (function() {
            function injectEmailAndClick() {
                const emailInput = document.querySelector('input[type="email"]') || 
                                 document.querySelector('input[placeholder*="email"]') ||
                                 document.querySelector('input[aria-label*="email"]');
                
                if (emailInput) {
                    console.log('Found email input, injecting email...');
                    emailInput.value = '$email';
                    emailInput.dispatchEvent(new Event('input', { bubbles: true }));
                    emailInput.dispatchEvent(new Event('change', { bubbles: true }));
                    
                    setTimeout(() => {
                        const continueButton = Array.from(document.getElementsByTagName('button'))
                            .find(button => 
                                button.textContent.toLowerCase().includes('continue') ||
                                button.textContent.toLowerCase().includes('next')
                            );
                        
                        if (continueButton) {
                            console.log('Found continue button, clicking...');
                            continueButton.click();
                        } else {
                            console.log('Continue button not found, retrying...');
                            setTimeout(injectEmailAndClick, 500);
                        }
                    }, 500);
                } else {
                    console.log('Email input not found, retrying...');
                    setTimeout(injectEmailAndClick, 500);
                }
            }
            injectEmailAndClick();
        })();
    """.trimIndent()

    fun buildOTPScript(otp: String): String = """
        (function() {
            function injectOTPAndSubmit() {
                const otpInput = document.querySelector('input[type="tel"]') ||
                               document.querySelector('input[aria-label*="digit code"]') ||
                               document.querySelector('input[placeholder*="verification"]');
                
                if (otpInput) {
                    console.log('Found OTP input, injecting code...');
                    otpInput.value = '$otp';
                    otpInput.dispatchEvent(new Event('input', { bubbles: true }));
                    otpInput.dispatchEvent(new Event('change', { bubbles: true }));
                    
                    setTimeout(() => {
                        const verifyButton = Array.from(document.getElementsByTagName('button'))
                            .find(button => 
                                button.textContent.toLowerCase().includes('verify') ||
                                button.textContent.toLowerCase().includes('continue')
                            );
                            
                        if (verifyButton) {
                            console.log('Found verify button, clicking...');
                            verifyButton.click();
                        }
                    }, 500);
                } else {
                    setTimeout(injectOTPAndSubmit, 500);
                }
            }
            injectOTPAndSubmit();
        })();
    """.trimIndent()

    fun detectAuthState(): String = """
        (function() {
            const otpInput = document.querySelector('input[type="tel"]') ||
                           document.querySelector('input[aria-label*="digit code"]');
            const emailInput = document.querySelector('input[type="email"]') ||
                             document.querySelector('input[placeholder*="email"]');
            const phoneInput = document.querySelector('input[placeholder*="phone"]') ||
                             document.querySelector('input[aria-label="Enter phone number or email"]');
            
            if (otpInput) {
                return 'OTP';
            } else if (emailInput) {
                return 'EMAIL';
            } else if (phoneInput) {
                return 'PHONE';
            }
            return 'UNKNOWN';
        })();
    """.trimIndent()
}