import json
data = json.load(open('atomist-registration.json'))

output = "atomist:\n"
for endpoint in data["endpoints"]:
    output += "  " + endpoint["name"][0].lower() + endpoint["name"][1:] + "Url: \"" + endpoint["url"] + "\"\n"

with open("application-local.yml", "w") as fh:
    fh.write(output)
