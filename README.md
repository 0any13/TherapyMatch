# Psychiatrist-Client Matching System

A multi-agent system based in JADE  that matches clients with psychiatrists based on symptoms, urgency, and preferences. Features 8 psychiatrists with 200 total appointment slots across weekdays.

## Installation

1. *(Java JDK 8+** and **JADE 4.6.0 needed)*

2. **Compile:**
   ```bash
   javac -cp "path/to/jade.jar" -d src\therapyMatch\ontology\*.java src\therapyMatch\agents\*.java src\therapyMatch\main\*.java src\therapyMatch\gui\*.java
   ```

3. **Run:**
   ```bash
   java -cp "path/to/jade.jar;out" therapyMatch.gui.TherapyMatchGUI
   ```
   *(Use `:` instead of `;` on Mac/Linux)*

## Usage

1. Click **"Start JADE System"** to initialize agents
2. Enter symptoms (e.g., `anxiety,insomnia`), urgency (1-5), and preference (online/in-person)
3. Click **"Submit Client Request"** to match with a psychiatrist
4. View appointment details in the system log


Each psychiatrist has 25 time slots (5 per day, Monday-Friday at 09:00, 11:00, 14:00, 16:00, 18:00).

## How It Works

The Coordinator Agent forwards client requests to all psychiatrists, who calculate match scores based on:
- **Specialty match** (0-50 pts)
- **Available slots** (0-30 pts)
- **Preference match** (0-20 pts)

The psychiatrist with the highest score gets the client, and the next available time slot is booked.