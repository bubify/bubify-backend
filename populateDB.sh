echo "Add all teachers and senior tas"
while IFS= read -r p
do
    echo `./scripts/post2.sh admin/add-teacher "${p}"`
done < teachers.csv

echo "Add all junior TAs"
while IFS= read -r p
do
    echo `./scripts/post2.sh admin/add-junior-ta "${p}"`
done < junior-tas.csv

echo "Add all students"
while IFS= read -r p
do
    echo `./scripts/post2.sh admin/add-student "${p}"`
done < students.csv

echo "Add all achievements"
while IFS= read -r p
do
    echo `./scripts/post2.sh admin/add-achievement "${p}"`
done < achievements.csv
