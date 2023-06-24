echo "Add all students"
while IFS= read -r p
do
    echo `./scripts/post2.sh admin/add-user "${p}"`
done < users.csv

echo "Add all achievements"
while IFS= read -r p
do
    echo `./scripts/post2.sh admin/add-achievement "${p}"`
done < achievements.csv
