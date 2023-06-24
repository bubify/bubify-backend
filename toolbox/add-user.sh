#!/usr/bin/zsh

echo "Add users from users.csv"
while IFS= read -r p
do
    echo `./post2.sh admin/add-user "${p}"`
done < users.csv
