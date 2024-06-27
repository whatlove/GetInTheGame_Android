# GetInTheGame_Android
Track players and teams in a packed gym.

The idea of this app is to keep games flowing in a crowded gym fairly and efficiently.

Teams are created automatically. The app was created for volleyball, so the team size is 6 by default and can be adjusted up to 8 as needed.
There will always be 2 teams with positions available. When a team fills a new team is automatically created.

Players add themselves by entering their name and a PIN. The PIN is used for verification to prevent other players from making changes to other players.
Players select which team to join from the 2 available teams. The player can unjoin their team by clicking on the team button next to their name.

The app was designed for a gym with 2 courts. Each team has a button for court 1 and court 2. Clicking one of these indicates that the team is entering that court.
The app ensures that teams enter the courts in order. If a newer team tries to enter the court before the next time in line the app will prompt to select the correct next team.
The ensures that there are only 2 teams on each court. If a new team is selected to enter a court that already has 2 teams on it the user will be prompted to select a team to remove from the court.
Teams can also be removed from the court by clicking the court button that the team is on.

The app keeps a log of each game. Log entries are created when a team leaves the court.
Future iterations will have the ability to keep score during the game. The score is included in the log.

Planned upgrades:
-Score screen to monitor court score and progress
-Centralized data
-Gym selection (to allow multiple concurrent iterations)
-Require geolocation verification
