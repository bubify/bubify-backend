#/usr/bin/zsh

echo "Add all achievements fom achievements.csv"
while IFS= read -r p
do
    echo `./post2.sh admin/add-achievement "${p}"`
done < achievements.csv
